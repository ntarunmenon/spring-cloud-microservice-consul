## To Test

Run `OrderServiceApplication` as a spring boot application and then 


```unix
curl --header "Content-Type: application/json" \
  --request POST \
--data '{"customerId":"1", "productId":"2", "quantity":"1"}' \
http://localhost:7071/order
```
The response will  be

```json
{"receiptNo":"ojftlxqpmg"}
```