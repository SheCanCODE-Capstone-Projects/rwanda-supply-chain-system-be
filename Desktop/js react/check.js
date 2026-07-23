function CheckString(value){
    if (typeof value !== 'string') {
        return false; 
    }
    const normalized = value.toLowerCase().replace(/[^a-z0-9]/g, '');
    const reversed = normalized.split('').reverse().join('');
    return normalized === reversed;
}
console.log(CheckString('Racecar'));
console.log(CheckString('hello'));
console.log(CheckString(123)); 