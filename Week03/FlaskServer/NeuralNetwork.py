import tensorflow as tf
import keras
from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import pandas as pd

df = pd.read_csv('students_placement.csv')
X = df.drop(columns=['placed'])
y = df['placed']

from sklearn.model_selection import train_test_split
X_train,X_test,y_train,y_test = train_test_split(X,y,test_size=0.2,random_state=2)

model = Sequential()
model.add(Dense(12,input_dim=3, activation='relu'))
model.add(Dense(24, activation='relu'))
model.add(Dense(12, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
model.fit(X_train,y_train,epochs=100, batch_size=10)

_, accuracy = model.evaluate(X_test,y_test)
print(accuracy)

import pickle
pickle.dump(model, open("nn.pkl", "wb"))



