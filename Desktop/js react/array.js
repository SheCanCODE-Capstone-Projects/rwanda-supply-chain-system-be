function arrystring(array){
    if (!Array.isArray(array)) {
        console.log('Input must be an array');
    }
    
    return array.map(item => String(item)).join(', ');
}


const arr = ['kigali', 'rwanda'];
console.log(arrystring(arr)); // kigali, rwanda