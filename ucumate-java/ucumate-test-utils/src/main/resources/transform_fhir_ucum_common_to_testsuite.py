import json

def transform(json_file, text_file):
  data = json.load(json_file)
  common_codes = data["compose"]["include"][0]["concept"]
  transformed_codes = []
  for idx, code in enumerate(common_codes):
    line_json = {
      "id": f"fhir-common-{idx+1}",
      "inputExpression": code["code"],
      "valid": True,
      "reason": code["display"]
    }
    text_file.write(json.dumps(line_json) + ",\n")

with open("fhir_common_ucum.json", 'r') as f1, open("fhir_common_ucum_2.json", 'r') as f2, open("text.txt", 'w') as f3:

  transform(f1, f3)
  # transform(f2, f3) f2 is empty for some reason...


  
