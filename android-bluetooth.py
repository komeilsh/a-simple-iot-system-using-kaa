#IN THE NAME OF GOD
#KOMEIL SHAH HOSSEINI
#READING TEMPERATURE AND HUMIDITY FROM DHT11 SENSOR AND SENDING IT TO ANDROID PHONE VIA BLUETOOTH

import time
import RPi.GPIO as GPIO
from bluetooth import *
import Adafruit_DHT


def read_temp():
	sensor = 11
	pin = 2
	humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)
	if humidity is not None and temperature is not None:
#		print temperature,humidity
		return str(temperature)+","+str(humidity)
	else:
		print('Failed to get reading. Try again!')

#while True:
#	print(read_temp())	
#	time.sleep(1)

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "AquaPiServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
while True:          
	print "Waiting for connection on RFCOMM channel %d" % port

	client_sock, client_info = server_sock.accept()
	print "Accepted connection from ", client_info

	try:
	        data = client_sock.recv(1024)
        	if len(data) == 0: break
	        print "received [%s]" % data

		if data == 'temp':
			data = str(read_temp())+'!'
			print data
		elif data == 'lightOn':
			GPIO.output(17,False)
			data = 'light on!'
		elif data == 'lightOff':
			GPIO.output(17,True)
			data = 'light off!'
		else:
			data = 'WTF!' 
	        client_sock.send(data)
		print "sending [%s]" % data

	except IOError:
		pass

	except KeyboardInterrupt:

		print "disconnected"

		client_sock.close()
		server_sock.close()
		print "all done"

		break
