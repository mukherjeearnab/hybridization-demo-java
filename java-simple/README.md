# Hybrid model — minimal Java version

The smallest possible illustration of the hybrid model:

```
{power, ambient_temp} -> ThermalModel (physics, Modelica) -> temp_sim
{temp_sim, power, ambient_temp} -> ResidualMLP (ML, PyTorch) -> residual_correction
temp_pred = temp_sim + residual_correction
```

Class hierarchy (following the UML in `../uml`):

```
ExecMIC                 exec.puml : name, description, ports, parameters
├── SimMIC              sim.puml  : + language, equation, solver
│   └── ThermalModel    the Modelica model (modelica/ThermalModel.mo)
├── MlMIC               ml.puml   : + framework, architecture (layers), generic forward pass
│   └── ResidualMLP     the PyTorch model (torch/model.py)
└── HybridModel         combines both: temp_pred = temp_sim + residual

Parameter               scalar or tensor value of a model
├── LearnedParameter    MLP weights and biases
└── NonTrainableState   feature normalisation mean / std
```

The MLP's weights are ordinary MIC parameters; `MlMIC.forward` computes the network purely from the
described layers and parameters (dummy values, 3 -> 4 -> 1 instead of the real 3 -> 32 -> 32 -> 1).

Run (Java 8+):

```sh
mkdir -p build && javac -d build src/hybrid/*.java
java -cp build hybrid.Main
```

For the full UML-based version (complete MIC classes, wiring validation, JSON export), see `../java`.
