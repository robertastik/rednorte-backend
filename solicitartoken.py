
import http.client

conn = http.client.HTTPSConnection("dev-robgo.us.auth0.com")

payload = "{\"client_id\":\"VJMeVoqkaqCIWT72DCv59QBUSkuua6A5\",\"client_secret\":\"fn8y2eI66qPrECY4NrrlY3H_JmKFEnvaz45gMJFlPU862aiaZ1g_HAR4wG4ElNFR\",\"audience\":\"https://api-demo-auth0/\",\"grant_type\":\"client_credentials\"}"

headers = { 'content-type': "application/json" }

conn.request("POST", "/oauth/token", payload, headers)

res = conn.getresponse()
data = res.read()

print(data.decode("utf-8"))