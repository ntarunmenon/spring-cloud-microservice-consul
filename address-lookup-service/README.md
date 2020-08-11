## To Test

Run `AddressLookupApplication` as a spring boot application and then 


```unix
curl 'localhost:7090/address?customerId=1'
```
The response will always be

```json
{
   "addressLine1":"fancy place",
   "addressLine2":"belonngs to 1%",
   "suburb":"Priv",
   "state":"UTOIPA"
}
```