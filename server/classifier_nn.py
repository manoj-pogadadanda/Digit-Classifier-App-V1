# -*- coding: utf-8 -*-
"""
Created on Sun Oct 30 14:07:35 2022

@author: Sumant Kulkarni
"""

import numpy as np
import matplotlib.pyplot as plt
import tensorflow as tf

def draw(n):
    plt.imshow(n,cmap=plt.cm.binary)
    plt.show()

#Load the MNIST dataset
mnist = tf.keras.datasets.mnist
#Split the dataset to train and validation
(x_train,y_train) , (x_test,y_test) = mnist.load_data()

#Preprocess the dataset
x_train = tf.keras.utils.normalize(x_train,axis=1)
x_test = tf.keras.utils.normalize(x_test,axis=1)

x_train_append = x_train
y_train_append = y_train

#Display images visually
draw(x_train_append[20])
print(y_train_append[20])
draw(x_train_append[15000])
print(y_train_append[15000])


#Build the Deep Learning Model from scratch
model = tf.keras.models.Sequential()
model.add(tf.keras.layers.Flatten(input_shape=(28, 28)))
model.add(tf.keras.layers.Dense(128,activation=tf.nn.relu))
model.add(tf.keras.layers.Dense(128,activation=tf.nn.relu))
model.add(tf.keras.layers.Dense(10,activation=tf.nn.softmax))
model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy']
              )
#Train the model using the dataset
#Check the weights of the Model and perform fine tuning if required
model.fit(x_train_append,y_train_append,epochs=10)

#Validate the trained model
print("\nTraining Evaluation:")
val_loss,val_acc = model.evaluate(x_train_append,y_train_append)

#Store the trained model
model.save('tf_classifier.h5')

#Load the trained model for testing
loaded_model = tf.keras.models.load_model('tf_classifier.h5')
print("\nPrediction using saved model:")
predictions=loaded_model.predict([x_test])
print('label -> ',y_test[2])
print('prediction -> ',np.argmax(predictions[2]))
draw(x_test[2])

#Check the accuracy of the model
print("\nTesting Evaluation: ")
val_loss,val_acc = loaded_model.evaluate(x_test,y_test)
