import json

def transform(json_file, text_file):
  data = json.load(json_file)
  common_codes = data["compose"]["include"][0]["concept"]
  transformed_codes = []
  for idx, code in enumerate(common_codes):
    print(code)
    transformed_codes.append(code["code"])
  json.dump(transformed_codes, text_file, indent=2)

with open("fhir_common_ucum.json", 'r') as f1, open("fhir_common_ucum_2.json", 'r') as f2, open("text.txt", 'w') as f3:

  transform(f1, f3)