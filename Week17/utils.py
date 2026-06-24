from PIL import Image
import numpy as np
import os
import matplotlib.pyplot as plt

def read_image_from_path(path, size):
    im = Image.open(path).resize(size)
    return np.asarray(im, dtype=np.float32)

def folder_to_images(folder, size=(224, 224)):
    
    list_dir = [folder + '/' + name for name in os.listdir(folder) if name.endswith((".jpg", ".png", ".jpeg"))]
    
    i = 0
    images_np = np.zeros(shape=(len(list_dir), *size, 3))
    images_path = []
    for path in list_dir:
        try:
            images_np[i] = read_image_from_path(path, size)
            images_path.append(path)
            i += 1
            
        except Exception:
            print("error: ", path)
#             os.remove(path)

    images_path = np.array(images_path)
    return images_np, images_path