# InBoundFlow

![alt text](diag/svg/inboundfow.uml.svg "Title")

## InBoundService
This is resposnible for recieving incoming user messages from thirdparty services and converting them to readable messages so that framework can consume and process it.
## InBoundFilter
This responsble for filtering incoming user message and to decide whether to forward it to InBoundRouter or not.
## InBoundRouter
Based on configuration *InBoundRuuter* decides wheather to handler it locally, give it to ChatBot or forward it to next service
## InBoundHandler
Message cane be processed and handled locally.
## BotEngine
Message cane be handed over to one the *BotController*s
## InBoundForward
Message can be forwarded to another service to handle it


