import base64
import cv2 
import threading
import time

HEADERSIZE = 10
windowName = "image"
heartBeatThreadWorking = False

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
    cv2.waitKey(1)
    
def sendData(msg, conn):
    #bytesToSend = myEncode(msg)
    #conn.sendall(bytesToSend)
    conn.sendall(msg)
    
def recvData(socket, conn):
    msg, address = conn.recvfrom(5120)
    #decode message to get the header to bytes
    msg = myDecode(msg)
    msg.decode("utf-8")
    
    print ("recived Data " + str(msg))
    
    return msg

def recvDataImage(socket, conn):
    msg, address = conn.recvfrom(1000000)
    #decode message to get the header to bytes
    msg = myDecode(msg)
    if (len(msg) < 1):
        return -1
    print("Recived Data")
    return msg

def heartBeatFunction(conn):
    while (heartBeatThreadWorking):
        sendData(bytearray(1), conn)
        time.sleep(1)
        print("BEEAAATT")

def startHearBeatThread(conn):
    global heartBeatThreadWorking
    heartBeatThreadWorking = True
    
    thrd = threading.Thread(target = heartBeatFunction, args = (conn, ))
    thrd.start()
    
def stopHearBeatThread():
    global heartBeatThreadWorking
    heartBeatThreadWorking = False