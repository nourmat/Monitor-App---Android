import socket
import UDPHelperFunction as helper

HEADERSIZE = 10
HOST = ""
PORT = 12000
imageName = "lolo.jpg"

s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
s.bind((HOST,PORT))
print("UDP server up and listening")

print("waiting to recive")
msg = helper.recvData(s)

while (True):
    print("waiting to recive")
    msg = helper.recvDataImage(s)
    helper.writeImg(imageName,bytearray(msg))
    helper.getAndShowImg(imageName)