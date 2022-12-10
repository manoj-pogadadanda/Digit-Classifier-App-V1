
from flask import Flask
from flask import request
import base64
import os

import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt

TIMEOUT = 400
PORT = 8080

app = Flask(__name__)

def draw(n):
    plt.imshow(n,cmap=plt.cm.binary)
    plt.show()
    
@app.route('/', methods=['POST', 'GET'])
#@app.route('/')
def process():
    if 'image' not in request.form:
        return 'Missing required image param' , 400
    elif 'filename' not in request.form:
        return 'Missing required filename param' , 400
    else:
        filename = request.form['filename']
        #decode base64 string data
        decoded_data=base64.b64decode((request.form['image']))
        #Convert image to tensor
        img_data = tf.image.decode_image(decoded_data)
        #Convert to grey scale
        img_data_grey = tf.image.rgb_to_grayscale(img_data)
        #Resize to required scale
        img_data_resized = tf.image.resize(img_data_grey, [28, 28])
        #Create the ndarray
        img_ndarray = np.array(img_data_resized).reshape(1,28,28)
        #Normalise the image data
        img_ndarray_norm = tf.keras.utils.normalize(img_ndarray,axis=1)
        #Pre process the image
        img_ndarray_norm_reverse = img_ndarray_norm.max() - img_ndarray_norm
        img_ndarray_norm_reverse[img_ndarray_norm_reverse < np.quantile(img_ndarray_norm_reverse, 0.33)] = 0
        #Load the trained model
        model = tf.keras.models.load_model('tf_classifier.h5')
        #Predict the digit
        predictions=model.predict(img_ndarray_norm_reverse)
        print('prediction -> ',np.argmax(predictions[0]))
        #draw(img_ndarray_norm_reverse[0])
        
        # Check if directory exists
        category = str(np.argmax(predictions[0]))        
        if not os.path.isdir(category):
            os.makedirs(category)
        #write the decoded data back to original format in  file
        img_file = open(os.path.join(os.getcwd(), category, filename) , 'wb')
        img_file.write(decoded_data)
        img_file.close()
        return 'Success', 200


# Start the web server
if __name__ == "__main__":
    app.secret_key = ".."
#    app.run(host = 'localhost',port = PORT, debug=False)
    app.run(host = '0.0.0.0',port = PORT, debug=False)
    