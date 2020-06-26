/* Submission form only requires 2 decimals, this function enforces that rule */
function setTwoNumberDecimal(event) {
    this.value = parseFloat(this.value).toFixed(2);
}