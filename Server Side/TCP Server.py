import socket 
import TCPHelperFunctionsWithoutHeaders as helper

HEADERSIZE = 10
HOST = ""
PORT = 12000
imageName = "lolo.jpg"

s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.bind((HOST,PORT))
print("TCP server up and listening")

s.listen(5)

while (True):
    print("Waiting for a connection")
    conn, addr = s.accept()
    print("Accepted a connection")
    
    print("Thread Started")
    helper.startHearBeatThread(conn)
    
    print("waiting to recive")
    msg = helper.recvData(s, conn)
    
    while (True):
        try:
            msg = helper.recvDataImage(s, conn)
            if msg == -1: # if connection lost wait for new connection
                break
            helper.writeImg(imageName,bytearray(msg))
            helper.getAndShowImg(imageName)
        except ConnectionResetError:
            break;
        except Exception:
            continue
            #template = "An exception of type {0} occurred. Arguments:\n{1!r}"
            #message = template.format(type(ex).__name__, ex.args)
            #print (message)
            
    helper.stopHearBeatThread()
    conn.close()