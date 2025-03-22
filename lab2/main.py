import ssl

import uvicorn
from fastapi import FastAPI

app = FastAPI()

@app.get("/")
def read_root():
    return  "KP-21 Hudz Vladyslav / Робота студента КП-21 Гудзь Владислава"


if __name__ == "__main__":
    uvicorn.run(
        app,
        host="localhost",
        port=8701,
        ssl_keyfile_password="changeit",
        ssl_keyfile="keys/localhost+2-key.pem",
        ssl_certfile="keys/localhost+2.pem",
        ssl_version=ssl.PROTOCOL_TLSv1_2,
        ssl_ciphers="RSA",
        )
