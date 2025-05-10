package com.tickeTeam.common.exception.customException;

import com.tickeTeam.common.exception.ErrorCode;

public class NotFoundException extends BusinessException {
  public NotFoundException(ErrorCode ec) {
    super(ec);
  }
}
