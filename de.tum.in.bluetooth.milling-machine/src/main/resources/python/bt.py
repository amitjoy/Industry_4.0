#!/usr/bin/python
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import bluetooth
from subprocess import Popen, PIPE
import sys
import time
import paho.mqtt.publish as publish
from random import randint

class BT(object):
    def __init__(self, receiveSize=1024):
        self.btSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self._ReceiveSize = receiveSize

    def __exit__(self):
        self.Disconnect()
        
    def Connect(self, mac, port = 3333):
        self.btSocket.connect((mac, port))

    def Disconnect(self):
        try:
            self.btSocket.close()
        except Exception:
            pass
        
    def Discover(self):
        btDevices = bluetooth.discover_devices(lookup_names = True)
        if (len(btDevices) > 0):
                return btDevices
        else:
            raise Exception('no BT device!')

    def DumpDevices(self, btDeviceList):
        for mac, name in btDeviceList:
            print("BT device name: {0}, MAC: {1}".format(name, mac))
    
    def BindListen(self, mac, port=3333, backlog=1):
        self.btSocket.bind((mac, port))
        self.btSocket.listen(backlog)
        
    def Accept(self):
        client, clientInfo = self.btSocket.accept()
        return client, clientInfo
        
    def Send(self, data):
        self.btSocket.send(data)
        
    def Receive(self):
        return self.btSocket.recv(self._ReceiveSize)
    
    def GetReceiveSize(self):
        return self._ReceiveSize
        
    
def StartIoTGateway(addr):
    cli = BT()
    print('BT Discovery...')
    mac = addr
    name = "MILLING-MACHINE"
    print('Connecting to : {0}, MAC: {1}'.format(name, mac))
    cli.Connect(mac)
    while True:
        try:
            data = cli.Receive()
            #As soon as we receive the real-time data, publish it to cloud
            publish.single("$EDC/tum/BLUETOOTH-V1/" + mac + "/data", data, hostname="iot.eclipse.org")
        except Exception as e:
            print(e.__str__())
            break
    cli.Disconnect()

def StartMillingMachine(addr):
    srv = BT()
    mac = addr
    srv.BindListen(mac)
    print('Listening for connections on: {0}'.format(mac))
    while True:
        client, clientInfo = srv.Accept()
        print('Connected to: {0}, port: {1}'.format(clientInfo[0], clientInfo[1]))
        try:
            while True:
                data = str(randint(1,1000));
                print("Broadcasting data..." + data)
                client.send(data)
                time.sleep(3)
        except:
            print("Closing Bluetooth socket")
        client.close()
    srv.Disconnect()

if __name__ == '__main__':
    cmd = sys.argv[1]
    if (cmd == 'machine'):
        StartMillingMachine(sys.argv[2])
    elif (cmd == 'gw'):
        StartIoTGateway(sys.argv[2])
    else:
        print("Milling Machine Communication Simulation")
        print("Copyright 2015 TU Munich")
        print("Please specify 'machine' or 'gw'")
        print("Milling Machine Communication Simulation")
