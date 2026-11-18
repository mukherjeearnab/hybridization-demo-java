# Hybrid model — minimal Java version

The smallest possible illustration of the hybrid model:

```
{power, ambient_temp} -> ThermalModel (physics, Modelica) -> temp_sim
{temp_sim, power, ambient_temp} -> ResidualMLP (ML, PyTorch) -> residual_correction
temp_pred = temp_sim + residual_correction
```

Project layout:

```
src/hybrid/
├── Main.java            entry point: prints the MICs and runs the simulation
├── description/         building blocks that describe any value (valueDescription.puml)
│                          ValueDescription, DataType, Unit, RangeDomain, Uncertainty
├── variables/           values flowing through a model
│                          Variable, MlVariable, Port, InternalVariable
├── parameters/          constants configuring a model
│                          Parameter, LearnedParameter, NonTrainableState, Hyperparameter
├── mic/                 the generic Model Identity Card classes (exec / sim / ml .puml)
│                          ExecMIC, SimMIC, MlMIC, Layer
└── models/              the concrete models of this demo
                           ThermalModel, ResidualMLP, HybridModel
```

Class hierarchy (following the UML in `../uml`):

```
ExecMIC                   exec.puml : name, description, ports, internal variables, parameters
├── SimMIC                sim.puml  : + language, equation, solver
│   └── ThermalModel      the Modelica model (modelica/ThermalModel.mo)
├── MlMIC                 ml.puml   : + framework, architecture (layers), generic forward pass
│   └── ResidualMLP       the PyTorch model (torch/model.py)
└── HybridModel           combines both: temp_pred = temp_sim + residual

Port                      exec.puml : named group of port Variables

ValueDescription          valueDescription.puml : name, description, dataType, unit,
│                           quantityKind, semanticIRI, defaultValue, validityDomain, uncertainty
├── Variable              port value, IN or OUT
│   └── MlVariable        + role (inputFeature / target ...)
├── InternalVariable      value computed inside a model
└── Parameter             constant configuring a model
    ├── LearnedParameter  MLP weights and biases (kind, count, initializer, frozen, storageRef)
    ├── NonTrainableState feature normalisation mean / std
    └── Hyperparameter    training choices (category, isTuned, tuningMethod)

DataType, Unit, RangeDomain, Uncertainty   building blocks of a ValueDescription
```

The MLP's weights are ordinary MIC parameters; `MlMIC.forward` computes the network purely from the
described layers and parameters (dummy values, 3 -> 4 -> 1 instead of the real 3 -> 32 -> 32 -> 1).
Running the program prints every model's identity card, then the hybrid simulation table.

Run (Java 8+):

```sh
mkdir -p build && javac -d build $(find src -name '*.java')
java -cp build hybrid.Main
```

For the full UML-based version (complete MIC classes, wiring validation, JSON export), see `../java`.
