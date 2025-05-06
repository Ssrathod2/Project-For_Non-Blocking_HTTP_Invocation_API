import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 1000, // virtual users
    duration: '20s', // run test for 30 seconds
};

export default function () {
    const baseUrl = 'http://localhost:8080/invokeasync';

    // === POST Request ===
    const postPayload = JSON.stringify({
        apiMethod: 'POST',
        timeout: 5000,
        requestDTO: {
            url: 'https://httpbin.org/post',
            bodyType: 'application/json',
            requestBody: JSON.stringify({ name: 'John Doe' }),
            headerVariables: {
                'Content-Type': 'application/json',
            }
        }
    });

    let res = http.post(baseUrl, postPayload, { headers: { 'Content-Type': 'application/json' } });
    check(res, { 'POST status 200': (r) => r.status === 200 });


    // === GET Request ===
    const getPayload = JSON.stringify({
        apiMethod: 'GET',
        timeout: 5000,
        requestDTO: {
            url: 'https://httpbin.org/get',
            headerVariables: {
                'Content-Type': 'application/json',
            }
        }
    });

    res = http.post(baseUrl, getPayload, { headers: { 'Content-Type': 'application/json' } });
    check(res, { 'GET status 200': (r) => r.status === 200 });


    // === PUT Request ===
    const putPayload = JSON.stringify({
        apiMethod: 'PUT',
        timeout: 5000,
        requestDTO: {
            url: 'https://httpbin.org/put',
            bodyType: 'application/json',
            requestBody: JSON.stringify({ updated: true }),
            headerVariables: {
                'Content-Type': 'application/json',
            }
        }
    });

    res = http.post(baseUrl, putPayload, { headers: { 'Content-Type': 'application/json' } });
    check(res, { 'PUT status 200': (r) => r.status === 200 });


    // === OPTIONS Request ===
    const optionsPayload = JSON.stringify({
        apiMethod: 'OPTIONS',
        timeout: 5000,
        requestDTO: {
            url: 'https://httpbin.org/anything',
            headerVariables: {
                'Origin': 'http://localhost',
            }
        }
    });

    res = http.request('POST', baseUrl, optionsPayload, { headers: { 'Content-Type': 'application/json' } });
    check(res, { 'OPTIONS status 200': (r) => r.status === 200 });
}
