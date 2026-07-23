function ArrayJoinWords(words){
    if (!Array.isArray(words)) {
    }

    return words.join(' ');
}

const words = ['she', 'can', 'code'];
console.log(ArrayJoinWords(words)); // 