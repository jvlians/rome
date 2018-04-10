## Usage

- Host documents on IPFS via the file upload functionality
- Share IPFS-hosted documents with other people using their public key
- Check what documents have been shared with you via Hyperledger and download them as necessary


## Prerequisite steps

1. Install and set up IPFS (https://ipfs.io/docs/install/)
2. Install and set up Hyperledger (https://ibm-blockchain.github.io/setup/)
    - NOTE: Setting up your own bluemix instance is only necessary if the network that
            this build supports is down or no long in service. If this is the case, or
            if you just want to build your own network, go to "Running your own network".


## (Optional) Running your own network

When creating the network, be sure to use the `rome-file-share-network.bna` document
included in this repository as your template for the network you create. This will
allow you to use the same chaincode we used for our network. Remember, taking this
route means using your own REST endpoint address. To make sure your copy of the program
is hitting the right address, simply change the address on line 45 of Main.java to
the address of your node's REST endpoint.


## Steps to run

1. Run `ipfs daemon` in terminal
2. Run Main.java