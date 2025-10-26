import time
import random
from flask import Blueprint, render_template, jsonify, request

main = Blueprint('main', __name__)

@main.route('/')
def index():
    return render_template('index.html')

@main.route('/api/process_ai', methods=['POST'])
def process_ai():
    # --- Simulation of AI Inference Process ---
    
    # 1. Mandatory Loading Period (Simulate long inference time)
    # The client-side must display the spinner during this delay.
    time.sleep(3)

    # 2. Simulate success/failure outcome (66% chance of success)
    if random.choice([True, True, False]): 
        return jsonify({
            'status': 'success',
            'message': 'AI inference completed successfully! The requested data has been processed.'
        }), 200
    else:
        # Return 500 status for clear client-side error handling
        return jsonify({
            'status': 'error',
            'message': 'Processing failed. Error code: ERR_INF_001. Please check input parameters.'
        }), 500