import json

try:
    with open('models_list.json', 'r', encoding='utf-16le') as f:
        data = json.load(f)
    
    print("Models supporting generateContent:")
    for model in data.get('models', []):
        methods = model.get('supportedGenerationMethods', [])
        if 'generateContent' in methods:
            print(f"- {model['name']} ({model.get('displayName', 'No Display Name')})")
except Exception as e:
    print(f"Error: {e}")
