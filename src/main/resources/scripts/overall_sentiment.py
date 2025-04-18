#!/usr/bin/env python3
import sys
import pandas as pd
import numpy as np
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.neural_network import MLPClassifier
import os

def predict_sentiment(scores):

    script_dir = os.path.dirname(os.path.realpath(__file__))
    csv_path = os.path.join(script_dir, "training_data.csv")
    df = pd.read_csv(csv_path)

    X = df.drop("label", axis=1).values.astype(float)
    y = df["label"].values

    encoder = LabelEncoder()
    y_encoded = encoder.fit_transform(y)

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    model = MLPClassifier(hidden_layer_sizes=(32,), activation='relu',
                          solver='adam', max_iter=1000, random_state=42)
    model.fit(X_scaled, y_encoded)

    # Validate input dimensions
    expected_num_features = X.shape[1]
    if len(scores) != expected_num_features:
        print(f"Expected {expected_num_features} scores but got {len(scores)}")
        sys.exit(1)

    today_scores = np.array([scores])
    today_scaled = scaler.transform(today_scores)
    prediction = model.predict(today_scaled)
    predicted_label = encoder.inverse_transform(prediction)[0]
    print(predicted_label)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Invalid Input: No scores provided")
        sys.exit(1)

    try:
        scores = [float(s.strip()) for s in sys.argv[1].split(",") if s.strip() != ""]
        predict_sentiment(scores)
    except Exception as e:
        print(f"Invalid Input: {str(e)}")
        sys.exit(1)
