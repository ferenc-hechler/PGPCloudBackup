# PGPCloudBackup

Constantly push changes in a folder recursively to a PGP encrypted Backup

# pCloud

# How to get the client-ID?

create a new app under: https://docs.pcloud.com/my_apps/



# How to get an access token (oauth2)

see https://docs.pcloud.com/methods/oauth_2.0/authorize.html


## query code

In a webbrowser enter the url with the client id of the application:

```
https://my.pcloud.com/oauth2/authorize?client_id=<client_id>&response_type=code
```

Response after login:

```
Your access code is:
<access_code>
Hostname:
eapi.pcloud.com
Please enter this code into the application.
This code will expire after 600 seconds.
```


## query access token for code

```
https://eapi.pcloud.com/oauth2_token?client_id=<client_di>&client_secret=<client_secret>&code=<code> 
```

Response:

```
{
  "result": 0,
  "userid": 1273851,
  "locationid": 2,
  "token_type": "bearer",
  "access_token": "..."
}
```
