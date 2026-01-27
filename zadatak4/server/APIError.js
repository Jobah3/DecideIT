module.exports = {
    NOT_REGISTERED: { message: 'User is not registered', code: 404 },
    NOT_LOGGED_IN:  { message: 'User is not logged in', code: 401 },
    OK:             { message: 'OK', code: 200 },
    NO_BODY:        { message: 'Wrong body parameters', code: 400 },
    NO_HEADER_PARAMS:{ message: 'No header parameters', code: 400 },
    ALREADY_EXISTS: { message: 'Already exists', code: 409 },
    INVALID_PASS:   { message: 'Invalid password', code: 400 },
    INVALID_PARAMS: { message: 'Invalid params', code: 400 },
};
