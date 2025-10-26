from flask import Flask
from app.routes import main as main_blueprint

def create_app():
    app = Flask(__name__)
    # NOTE: In a production environment, this should be loaded from environment variables.
    app.config['SECRET_KEY'] = 'archon_secure_key_12345'
    
    # Register Blueprints
    app.register_blueprint(main_blueprint)
    
    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=True)