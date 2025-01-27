import tensorflow as tf
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from tensorflow.keras.models import Sequential, load_model
from tensorflow.keras.layers import Dense
from tensorflow.keras.optimizers import Adam
import joblib
import json
import sys
import warnings
warnings.simplefilter("ignore")

# Global variables to hold model and scaler
loaded_model = None
loaded_scaler = None

def load_model_and_scaler():
    global loaded_model, loaded_scaler
    try:
        if loaded_model is None or loaded_scaler is None:
            loaded_model = load_model('imu_model100.h5')
            loaded_scaler = joblib.load('scaler100.pkl')
            #print("Model and scaler loaded successfully.")
        else:
            print("Using previously loaded model and scaler.")
    except:
        print("Model and scaler not found.")
    
    return loaded_model, loaded_scaler

def generate_next_values(prev_rows, model, scaler):
    columns_for_training = ['GyroX', 'GyroY', 'GyroZ', 'AccelerometerX', 'AccelerometerY', 'AccelerometerZ']
    # Select the previous rows of data
    prev_data = prev_rows[columns_for_training]

    # Normalize the data using the scaler
    normalized_data = scaler.transform(prev_data)

    # Use the model to predict the next values
    predicted_values = model.predict(np.array([normalized_data[-1]]),verbose = 0)

    # Inverse transform the predicted values to get them back to the original scale
    predicted_values = scaler.inverse_transform(predicted_values)

    # Extract the predicted values
    next_values = predicted_values[0]

    return next_values

if __name__ == "__main__":
    # Load the model and scaler
    model, scaler = load_model_and_scaler()

    # Parse input data from command-line arguments
    input_data = json.loads(sys.argv[1])

    # Convert input data to DataFrame
    prev_rows = pd.DataFrame(input_data['data'], columns=input_data['columns'])

    # Generate next predicted values
    next_values = generate_next_values(prev_rows, model, scaler)

    # Print the next predicted values
    #print("Next predicted values: !", next_values,"!")  # Convert numpy array to list for JSON serialization
    print("!", next_values,"!")  # Convert numpy array to list for JSON serialization



