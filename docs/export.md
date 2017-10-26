# Exporting Models for Use with DMS

When you export a model in MXNet, you will have a `model-symbol.json` file (1), which describes the neural network, and a larger `model-0000.params` file containing the parameters and their weights (2). In addition to these two files, for DMS to work with your model, you must provide a `signature.json` file (3), which describes your inputs and your outputs. You also have *the option* of providing labels for the outputs in a `synset.txt` file (4). For the purpose of a quick example, we'll pretend that you've already saved a checkpoint which gives you the first two assets by providing those files for you to download, or that you've acquired the trained models from a [model zoo](). We'll also provide the latter two files that you would create on your own based on the model you're trying to serve. Don't worry if that sounds ominous; creating those last two files is easy. More details on this can be found in later the **Required Assets** section.

TODO: add zoo link

Each of these files is viewable in a text editor. Download each or download and extract the provided zip file then review them to note the following features:

* [model-example.zip]() - contains the following four files
* [resnet-18-symbol.json]() - contains the layers and overall structure of the neural network; the name, or prefix, here is "resnet-18"
* [resnet-18-0000.params]() - contains the parameters and the weights; again, the prefix is "resnet-18"
* [signature.json]() - defines the inputs and outputs that DMS is expecting to hand-off to the API
* [synset.txt]() - an *optional* list of labels (one per line)

TODO: add S3 links to assets

Given these files you can use the `deep-model-export` CLI to generate a `.model` file that can be used with DMS. This file is essentially a zip archive, so changing the extension from `.model` to `.zip` will let you manually extract the files from any DMS model file.

To try this out, open your terminal and go to the folder you just extracted. Using the zip file and its directory structure can help you keep things organized. In this next example we'll go into the `model-example` folder and run `deep-model-export`. We're going to tell it our model's prefix is `resnet-18` with the `model-name` argument. Then we're giving it the `model-path` to the model's assets. These are all in the `models/resnet-18` folder.

```bash
cd model-example
deep-model-export --model-name resnet-18 --model-path models/resnet-18
```

This will output `resnet-18.model` in the current working directory.

## Deep Model Export Command Line Interface

Now let's cover the details on using `deep-model-export`. This CLI can take model checkpoints and package them into a `.model` file that can then be redistributed and served by anyone using DMS.

Example usage with the resnet-18 model you may have downloaded or exported in the [main README's](../README.md) examples:

```bash
deep-model-export --model-name resnet-18 --model-path models/resnet-18
```

### Arguments

```bash
$ deep-model-export -h
usage: deep-model-export [-h] --model-name MODEL_NAME --model-path MODEL_PATH

Deep Model Export

optional arguments:
  -h, --help            show this help message and exit
  --model-name MODEL_NAME
                        Exported model name. Exported file will be named as
                        model-name.model and saved in current working
                        directory.
  --model-path MODEL_PATH
                        Path to the folder containing model related files.
                        Signature file is required
```

1. model-name: required, prefix of exported model archive file.
2. model-path: required, directory which contains files to be packed into exported archive.

### Required Assets

#### Assets Overview
In order for the model file to be created, you need to provide these three or four assets:

1. signature.json - required; the inputs and outputs of the model
1. name-symbol.json - required; the model's definition (layers, etc.); name is any prefix you give it
1. name-0000.params - required; the model's hyper-parameters and weights; name must match the name from the previous JSON file
1. synset.txt - optional; a list of names of the prediction classes

**signature.json**

1. **input**: Contains MXNet model input names and input shapes. It is a list contains { data_name : name, data_shape : shape } maps. Client side inputs should have the same order with the input order defined here.
1. **input_type**: Defines the MIME content type for client side inputs. Currently all inputs must have the same content type and only two MIME types, "image/jpeg" and "application/json", are supported.
1. **output**: Similar to input, it contains MXNet model output names and output shapes.
1. **output_type**: Similar to input_type. Currently all outputs must have the same content type. Only two MIME types are currently supported: "image/jpeg" and "application/json".

   Using the resnet-18 example, you can view the `signature.json` file in the folder that was extracted once you dowloaded and served the model for the first time. The input is an image with 3 color channels and size 224 by 224. The output is named 'softmax' with length 1000 (one for every class that the model can recognize).

   ```json
   {
     "inputs": [
       {
         "data_name": "data",
         "data_shape": [0, 3, 224, 224]
       }
     ],
     "input_type": "image/jpeg",
     "outputs": [
       {
         "data_name": "softmax",
         "data_shape": [0, 1000]
       }
     ],
     "output_type": "application/json"
   }
   ```

   The `data_shape` is a list of integers. It should contain batch size as the first dimension as in NCHW. Also, 0 is a placeholder for MXNet shape and means any value is valid. Batch size should be set as 0.

**name-symbol.json**

  This is the model's definition in JSON format. You can name it whatever you want, using a consistent prefix. The pattern expected is `my-awesome-network-symbol.json` or `mnist-symbol.json` so that when you use `deep-model-export` you're passing in the prefix and it'll look for prefix-symbol.json. You can generate this file in a variety of ways, but the easiest for MXNet is to use the `.export` feature or the `mms.export_model` method described later.

**name-0000.params**

  This is the model's hyper-parameters and weights. It will be created when you use MXNet's `.export` feature or the `mms.export_model` method described later.

**synset.txt**

  This optional text file is for classification labels. Simply put, if it were for MNIST, it would be 0 through 9 where each number is on its own line. For a more complex example take a look at the [synset for Imagenet-11k](https://github.com/tornadomeet/ResNet/blob/master/predict/synset.txt).


   If `synset.txt` is included in exported archive file and each line represents a category, `MXNetBaseModel` will load this file and create `labels` attribute automatically. If this file is named differently or has a different format, you need to override `__init__` method and manually load it.


## Using Your Own Trained Models and Checkpoints

While all of these features are super exciting you've probably been asking yourself, so how do I create these fabulous DMS model files for my own trained models? We'll provide some MXNet code examples for just this task.

There are two main routes for this: 1) export a checkpoint or use the new `.export` function, or 2) using a DMS Python class to export your model directly.

The Python method to export model is to use `export_serving` function while completing training:

```python
   import mxnet as mx
   from mms.export_model import export_serving

   mod = mx.mod.Module(...)
   # Training process
   ...

   # Export model
   signature = { "input_type": "image/jpeg", "output_type": "application/json" }
   export_serving(mod, 'resnet-18', signature, aux_files=['synset.txt'])
```

Another route is to use some new features in MXNet.

```python
net = gluon.nn.HybridSequential() # this mode will allow you to export the model
with net.name_scope():
    net.add(gluon.nn.Dense(128, activation="relu")) # an example first layer
    # then add the rest of your architecture
net.hybridize() # hybridize your network so that it can be exported as symbols
# then train your network
net.export('models/mnist') #export your model to a specific path
```

Note: be careful with versions. If you export a v0.12 model and try to run it with DMS running v0.11 of MXNet, the server will probably throw errors and you won't be able to use the model.