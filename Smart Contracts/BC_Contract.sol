pragma solidity >=0.7.0 <0.9.0;

contract BCContract {
    event Gamble(bool odd, uint lastTimestamp, uint newValue);
    uint id;

    function gamble(uint id, bool odd, uint lastTimestamp, uint value) public payable returns (uint) {
        uint newValue;
        if(odd) {
            newValue = value * 2;
        } else {
            newValue = value / 2;
        }
        emit Gamble(odd, lastTimestamp, newValue);
        return newValue;
    }

}

contract EFContract {
}