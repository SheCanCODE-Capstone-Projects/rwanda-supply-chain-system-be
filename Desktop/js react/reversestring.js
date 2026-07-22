function reveserstring(str){
    if (typeof str !== 'string') {
        throw new TypeError('Input must be a string');
    }
    return str.split('').reverse().join('');
}


console.log(reveserstring('hello')); 
