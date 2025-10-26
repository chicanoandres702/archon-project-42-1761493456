import time
import random
import logging
from typing import Any, Dict
# Assuming relative import path from project root is correctly set up
from src.utils.performance_logger import log_performance

logger = logging.getLogger(__name__)

# Ensure logging is configured for demonstration
if not logger.handlers:
    logging.basicConfig(level=logging.INFO, format='%(asctime)s | %(levelname)s | %(name)s | %(message)s')

class ModelInferenceService:
    """
    Handles model loading and running inference, applying performance logging.
    This simulates the core inference worker logic.
    """
    
    def __init__(self, model_path: str):
        self.model_path = model_path
        logger.info(f"Initializing ModelInferenceService for model: {model_path}")
        # Simulate model loading time/setup
        time.sleep(0.1)
        self.is_ready = True
        
    @log_performance(task_name="ModelInferenceExecution")
    def run_inference(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Executes the model inference step and logs performance metrics.
        """
        if not self.is_ready:
            raise RuntimeError("Model is not loaded.")
        
        # --- Start of Inference Logic ---
        
        # Simulate computation time based on input complexity (e.g., batch size, tensor size)
        input_size = len(input_data.get('features', []))
        processing_time_base = input_size / 500.0
        processing_time = processing_time_base + (random.random() * 0.1)
        
        # Simulate computation
        time.sleep(processing_time)
        
        # Simulated result calculation
        prediction_value = sum(input_data.get('features', [0])) / 100.0
        
        # Simulated result. Note: Sensitive input data (input_data) is not logged here.
        output = {
            "prediction": [prediction_value, random.uniform(0, 1)],
            "metadata": {
                "request_id": "req-" + str(random.randint(1000, 9999))
            }
        }
        
        # --- End of Inference Logic ---
        
        return output

# Example Usage:
if __name__ == '__main__':
    
    inference_worker = ModelInferenceService(model_path="/mnt/models/super_model_v3.pkl")
    
    # Run inference multiple times to observe timing and peak memory tracking
    for i in range(3):
        logger.info(f"\n--- Running Inference Cycle {i+1} ---")
        sample_input = {"features": [random.randint(1, 100) for _ in range(100 + i * 50)]}
        
        # Run inference
        result = inference_worker.run_inference(sample_input)
        
        logger.info(f"Cycle {i+1} complete. Prediction output snippet: {result['prediction'][0]:.4f}")
