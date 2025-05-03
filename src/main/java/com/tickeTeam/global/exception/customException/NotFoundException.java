package com.tickeTeam.global.exception.customException;

import com.tickeTeam.global.exception.ErrorCode;

public class NotFoundException extends BusinessException {
  public NotFoundException(ErrorCode ec) {
    super(ec);
  }
}
