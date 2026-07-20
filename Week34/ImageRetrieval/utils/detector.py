import torch
from models.experimental import attempt_load
import cv2
import numpy as np

class YOLO:
    def __init__(self, weight_path, conf_thres=0.1, iou_thres=0.4, num_cls=80):
        self.conf_thres = conf_thres
        self.iou_thres = iou_thres
        self.num_cls = num_cls
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.model = attempt_load(weight_path, self.device)
        self.model.eval()
    
    def preprocess(self, image, input_size=(640,640), swap=(2, 0, 1)):
        padded_img = np.ones((input_size[0], input_size[1], 3)) * 114.0 if len(image.shape) == 3 else np.ones(input_size) * 114.0
        img = np.array(image)
        r = min(input_size[0] / img.shape[0], input_size[1] / img.shape[1])
        resized_img = cv2.resize(img, (int(img.shape[1] * r), int(img.shape[0] * r)), interpolation=cv2.INTER_LINEAR).astype(np.float32)
        padded_img[: int(img.shape[0] * r), : int(img.shape[1] * r)] = resized_img
        padded_img = padded_img[:, :, ::-1]
        padded_img /= 255.0
        padded_img = padded_img.transpose(swap)
        padded_img = np.ascontiguousarray(padded_img, dtype=np.float32)
        padded_img = torch.from_numpy(padded_img)[None].to(self.device)
        return padded_img, r

    def postprocess(self, predictions, ratio):
        boxes = predictions[:, :4]
        scores = predictions[:, 4:5] * predictions[:, 5:]
        boxes_xyxy = np.ones_like(boxes)
        boxes_xyxy[:, 0] = boxes[:, 0] - boxes[:, 2] / 2.
        boxes_xyxy[:, 1] = boxes[:, 1] - boxes[:, 3] / 2.
        boxes_xyxy[:, 2] = boxes[:, 0] + boxes[:, 2] / 2.
        boxes_xyxy[:, 3] = boxes[:, 1] + boxes[:, 3] / 2.
        boxes_xyxy /= ratio
        dets = multiclass_nms(boxes_xyxy, scores, nms_thr=0.45, score_thr=self.iou_thres)
        return dets

    def predict_one(self, img):
        img_preprocess, ratio = self.preprocess(img)
        with torch.no_grad():
            pred = self.model(img_preprocess)[0].cpu().numpy()
        
        predictions = np.reshape(pred, (1, -1, int(5 + self.num_cls)))[0]
        dets = self.postprocess(predictions, ratio)
        boxes, scores, clss = dets[:, :4], dets[:, 4], dets[:, 5]
        return boxes, scores, clss

    def predict_batch(self, imgs):
        return [self.predict_one(img) for img in imgs]
    
    def predict(self, img):
        if isinstance(img, list):
            return self.predict_batch(img)
        return self.predict_one(img)

def nms(boxes, scores, nms_thr):
    """Single class NMS implemented in Numpy."""
    x1 = boxes[:, 0]
    y1 = boxes[:, 1]
    x2 = boxes[:, 2]
    y2 = boxes[:, 3]

    areas = (x2 - x1 + 1) * (y2 - y1 + 1)
    order = scores.argsort()[::-1]

    keep = []
    while order.size > 0:
        i = order[0]
        keep.append(i)
        xx1 = np.maximum(x1[i], x1[order[1:]])
        yy1 = np.maximum(y1[i], y1[order[1:]])
        xx2 = np.minimum(x2[i], x2[order[1:]])
        yy2 = np.minimum(y2[i], y2[order[1:]])

        w = np.maximum(0.0, xx2 - xx1 + 1)
        h = np.maximum(0.0, yy2 - yy1 + 1)
        inter = w * h
        ovr = inter / (areas[i] + areas[order[1:]] - inter)

        inds = np.where(ovr <= nms_thr)[0]
        order = order[inds + 1]

    return keep


def multiclass_nms(boxes, scores, nms_thr, score_thr):
    """Multiclass NMS implemented in Numpy"""
    final_dets = []
    num_classes = scores.shape[1]
    for cls_ind in range(num_classes):
        cls_scores = scores[:, cls_ind]
        valid_score_mask = cls_scores > score_thr
        if valid_score_mask.sum() == 0:
            continue
        else:
            valid_scores = cls_scores[valid_score_mask]
            valid_boxes = boxes[valid_score_mask]
            keep = nms(valid_boxes, valid_scores, nms_thr)
            if len(keep) > 0:
                cls_inds = np.ones((len(keep), 1)) * cls_ind
                dets = np.concatenate(
                    [valid_boxes[keep], valid_scores[keep, None], cls_inds], 1
                )
                final_dets.append(dets)
    if len(final_dets) == 0:
        return None
    return np.concatenate(final_dets, 0)