import {fakeApi} from 'src/api/fake';
import {apiV1} from 'src/api/v1';

export const api = process.env.PROD ? apiV1 : fakeApi
