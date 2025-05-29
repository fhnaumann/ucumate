import pandas as pd
import json

# Load the Excel file
df = pd.read_excel("TableOfExampleUcumCodesForElectronicMessaging.xlsx", header=None, skiprows=2)  # skip header row

# Open output file
with open("TableOfExampleUcumCodesForElectronicMessaging.json", "w") as out:
    for i, row in df.iterrows():
        code = str(row[1]).strip()
        if not code:
            continue

        test_case = {
            "id": f"ucum-org-{int(row[0])}",  # Row number
            "inputExpression": code,       # UCUM_CODE
            "valid": True,
            "reason": str(row[2]).strip()  # Description
        }
        out.write(json.dumps(test_case) + ",\n")
