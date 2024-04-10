OC interface is an adaption of ME interface from applied energistics 2.
It basically do the same thing as the ME interface but with some slight differences.
It permits more encoded patterns, it can be configured using computers from OpenComputers,
and what's more, when patterns are used, it can broadcast a network packet on the network
(OpenComputers'), if properly configured.

Note that you don't need an adaptor to use this block, it can and probably always should
be directly connected to both Applied Energistics 2 network and the OpenComputers' network.

***

First, connect OC interface with both an AE2 network and an OpenComputers network.  
![](1.png)  
Then, put an encoded pattern in its config slot.  
![](2.png)  
Use lua script to configure the pattern.  
![](3.png)  
Don't forget to set broadcast port and open that port on modem.  
![](4.png)  
Then, when the pattern is used by AE2 network, the interface automatically broadcast a
message with the content "test" on port 1024.  

You can use all sorts of methods to make use of this message. Here's an example.
![](5.png)  

Note that the ingredient will be send to inventory inside the interface, rather than pushed
to containers nearby. Besides, the top side of the interface is connected to config slot, while
others are connected to the inventory for AE2 ingredient.
![](6.png)
