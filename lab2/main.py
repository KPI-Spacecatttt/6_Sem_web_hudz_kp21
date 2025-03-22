import ssl
import uvicorn
from fastapi import FastAPI

app = FastAPI()

@app.get("/")
def read_root():
    return  "Робота студента КП-21 Гудзь Владислава"

ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
ssl_context.load_cert_chain(certfile="keys/localhost+2.pem", keyfile="keys/localhost+2-key.pem")

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="localhost",
        port=8701,
        ssl_keyfile="keys/localhost+2-key.pem",
        ssl_certfile="keys/localhost+2.pem"
        )
