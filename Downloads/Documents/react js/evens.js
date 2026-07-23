function isEven(num) {
    if (num % 2 === 0) {
        console.log("This is divisible by 2");
        return "Even";
    } else {
        console.log("This is not divisible by 2");
        return "Odd";
    }
}

let result = isEven(8);
console.log(result); 