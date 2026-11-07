# ucumate-port-parity

Shared inputs and checks that keep the ports (Java, Go) consistent. The Java implementation is the reference.

| Folder | Contents |
|---|---|
| `grammar/` | `NewUCUM.g4` — every port generates its parser from this file |
| `ucum-essence/` | UCUM definition files, one per supported UCUM version |
| `test-suites/` | Functional suites: `UcumFunctionalTests.xml` (official), `ucum-tests.json` |
| `golden/` | Expected outputs generated from the Java implementation (planned) |
| `scripts/` | Tools to generate and compare golden files (planned) |

Only change files here deliberately: every port is affected.
