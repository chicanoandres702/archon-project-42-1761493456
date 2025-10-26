import time
import logging
import resource
from functools import wraps
from typing import Optional

logger = logging.getLogger(__name__)

def get_peak_memory_mib() -> Optional[float]:
    """
    Attempts to retrieve the peak Resident Set Size (RSS) usage of the process.
    Returns memory usage in MiB, or None if not supported.
    """
    try:
        # ru_maxrss is typically reported in kilobytes (kB) on Linux/macOS
        usage = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
        
        # Convert to MiB (1 MiB = 1024 kB)
        return usage / 1024
    except (NotImplementedError, AttributeError, ValueError):
        # resource.getrusage might not be fully supported on all platforms (e.g., Windows)
        return None

def log_performance(task_name: str):
    """
    Decorator to log execution time and process memory usage for a function.
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            start_time = time.perf_counter()
            
            # Note on memory tracking:
            # resource.getrusage().ru_maxrss reports the maximum RSS reached by the process
            # since it started. If the inference worker runs continuously, this reflects
            # the lifetime peak, which is a good indicator of footprint if garbage collection
            # is prompt, but not necessarily the memory increase *during* the call.
            # For simplicity and robust standard library usage, we log this peak value.
            
            # --- Function Execution ---
            result = func(*args, **kwargs)
            
            # --- Timing ---
            end_time = time.perf_counter()
            elapsed_time_ms = (end_time - start_time) * 1000

            # --- Memory Tracking (Peak) ---
            peak_mem_mib = get_peak_memory_mib()

            # --- Logging ---
            log_data = {
                "task": task_name,
                "duration_ms": f"{elapsed_time_ms:.3f}"
            }
            
            memory_log = "N/A"
            if peak_mem_mib is not None:
                log_data["peak_memory_mib"] = f"{peak_mem_mib:.2f}"
                memory_log = f"{peak_mem_mib:.2f}MiB"
                
            # Sensitive Data Handling Note: Ensure input/output data is not logged here.
            logger.info(
                f"PERF_LOG: [{task_name}] "
                f"Duration={log_data['duration_ms']}ms, "
                f"Peak_Mem={memory_log}"
            )
            
            return result
        return wrapper
    return decorator
