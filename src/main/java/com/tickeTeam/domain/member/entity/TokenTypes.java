package com.tickeTeam.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenTypes {
  ACCESS("access-token", "access"),
  REFRESH("refresh-token", "refresh");

  private final String type;

  private final String name;
}
