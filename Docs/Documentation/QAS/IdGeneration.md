# Quality Attribute Scenario: ID Generation Flexibility

| Element   | Statement regarding the need for ID generation.                                                                               |
|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| Stimulus  | Creation of a new entity                                                                                                      | 
| Stimulus Source | Code path that creates entities                                                                                               | 
| Environment | System (runtime)                                                                                                              | 
| Artifact  | `IdGenerator` (interface), `IdGeneratorFactory`, implementações: `IdGeneratorBase65EncodeImpl`, `IdGeneratorTimestampHexImpl` | 
| Response  | Depending on the chosen profile (`base65` or `timestamp_hex`), the ID is generated                                            |
