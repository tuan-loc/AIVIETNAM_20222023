from flask import Flask, request, jsonify
import numpy as np
import pickle

app = Flask(__name__)

model = pickle.load(open('model_new.pkl', 'rb'))


@app.route('/test')
def hello_world():
    return 'Hello World!'


@app.route('/predict', methods=['POST'])
def predict():
    cgpa = request.form.get('cgpa')
    iq = request.form.get('iq')
    profile_score = request.form.get('profile_score')
    input_query = np.array([[cgpa, iq, profile_score]])
    # results = {'cgpa':cgpa, 'iq':iq, 'profile_score':profile_score}
    print(model.predict(input_query))
    result = model.predict(input_query)[0]
    return jsonify({'placement': str(result)})
    # return jsonify(results)


@app.route('/predicttest', methods=['POST'])
def predicttest():
    cgpa = request.form.get('cgpa')
    iq = request.form.get('iq')
    profile_score = request.form.get('profile_score')
    results = {'cgpa': cgpa, 'iq': iq, 'profile_score': profile_score}
    return jsonify(results)


if __name__ == '__main__':
    app.run()
