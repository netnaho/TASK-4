package com.pharmaprocure.portal.mapper;

public interface BaseMapper<S, T> {
    T map(S source);
}
