# -*- coding: utf-8 -*-
"""
Created on Thu Dec 26 23:38:20 2019

@author: Nour E-Din
"""

import numpy as np
import base64
import cv2 

HEADERSIZE = 10
windowName = "image"

def myEncode(data):
    return base64.b64encode(data)

def myDecode(data):
    return base64.b64decode(data)

def writeImg(filename, data):
    file = open(filename,'wb')
    file.write(data)
    
def getAndShowImg(filename):
    img = cv2.imread(filename,1)
    cv2.namedWindow(windowName)        # Create a named window
    cv2.moveWindow(windowName, 0,0)  # Move it to (40,30)
    cv2.imshow(windowName,img)
    cv2.waitKey(10)
    
def showImg(data):
    # CV2
    nparr = np.fromstring(data, np.uint8)
    img_np = cv2.imdecode(nparr, cv2.CV_LOAD_IMAGE_COLOR) # cv2.IMREAD_COLOR in OpenCV 3.1
    # CV
    img_ipl = cv2.CreateImageHeader((img_np.shape[1], img_np.shape[0]), cv2.IPL_DEPTH_8U, 3)
    cv2.SetData(img_ipl, img_np.tostring(), img_np.dtype.itemsize * 3 * img_np.shape[1])
    cv2.imshow("image", img_ipl)
    
    
def sendData(msg, socket, address):
    msg = f'{len(msg):<{HEADERSIZE}}'+msg
    bytesToSend = msg.encode()
    socket.sendto(bytesToSend, address)
    
def recvData(socket, conn):
    full_msg = ""
    new_msg = True

    while (True):    
        msg = conn.recv(5120)
        #decode message to get the header to bytes
        msg = myDecode(msg)
        
        if new_msg:
            try:
                msglen = int(msg[:HEADERSIZE])
            except:
                break;
            print(msglen)
            new_msg = False
        
        full_msg += msg.decode("utf-8")
        
        if len(full_msg)-HEADERSIZE == msglen:
            print("full msg recived")
            print(full_msg[HEADERSIZE:])
            new_msg = True
            break
    return full_msg[HEADERSIZE:]

def recvDataImage(socket, conn):
    full_msg = []
    new_msg = True

    while (True):    
        msg = conn.recv(1000000)
        #decode message to get the header to bytes
        msg = myDecode(msg)
        
        if new_msg:
            msglen = int(msg[:HEADERSIZE])
            print(msglen)
            new_msg = False
        
        full_msg += msg
        
        if len(full_msg)-HEADERSIZE == msglen:
            print("full msg recived")
            print(full_msg[HEADERSIZE:])
            new_msg = True
            break
    return full_msg[HEADERSIZE:]