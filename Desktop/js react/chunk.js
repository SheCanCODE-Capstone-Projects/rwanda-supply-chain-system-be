function ArraySplits(array, size){
    if (!Array.isArray(array)) {
    }
    if (typeof size !== 'number' || size <= 0) {
    
    }

    const chunks = [];
    for (let i = 0; i < array.length; i += size) {
        chunks.push(array.slice(i, i + size));
    }
    return chunks;
}
console.log(ArraySplits([1,2,3,4,5], 2));