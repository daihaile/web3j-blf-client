pragma solidity >=0.7.0 <0.9.0;

import "./BC_Contract.sol";

contract Init {
    event Init(uint id, address _addr, uint amount, uint timestamp, bool odd);
    event NewValue(uint newValue);

    uint public id;
    uint public lastTimestamp;
    address public bcContract;


    mapping(address => uint) public values;


     function get(address _addr) public view returns (uint) {
        // Mapping always returns a value.
        // If the value was never set, it will return the default value.
        return values[_addr];
    }

    function setBCContract(address _addr) public {
        bcContract = _addr;
    }

    function deposit() public payable {
        require(bcContract != 0x0000000000000000000000000000000000000000);
        id++;

        values[msg.sender] = msg.value;
        lastTimestamp = block.timestamp;
        bool odd = true;
        if(lastTimestamp % 2 == 0) {
            odd = false;
        }
        emit Init(id, bcContract, msg.value, lastTimestamp, odd);
        uint newValue = callNextFunction(bcContract, odd, msg.value);
        values[msg.sender] = newValue;

    }

    function callNextFunction(address bcContractAddress, bool odd, uint256 value) private returns(uint) {
        BCContract bc = BCContract(bcContractAddress);
        address _to = payable(address(bcContractAddress));
        uint newValue = bc.gamble(id,odd,lastTimestamp,value);
        emit NewValue(newValue);
        return newValue;
    }

    fallback() external payable { }
    receive() external payable { }
}

