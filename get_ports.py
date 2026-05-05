import os
import re

for root, dirs, files in os.walk('.'):
    if 'application.yml' in files and 'src' in root:
        path = os.path.join(root, 'application.yml')
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
            m = re.search(r'port:\s*(\d+)', content)
            if m:
                print(f"{os.path.basename(os.path.dirname(os.path.dirname(os.path.dirname(root))))}: {m.group(1)}")
