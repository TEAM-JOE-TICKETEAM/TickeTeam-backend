package com.tickeTeam.domain.sectionPrice.entity;

import com.tickeTeam.domain.stadium.entity.Stadium;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌석의 가격은 구역별로 나뉨
 * 구역마다의 가격을 가지고 있는 테이블
 */

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SectionPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_stadium")
    private Stadium sectionStadium;

    @Column(name = "seat_section")
    private String seatSection;

    @Column(name = "section_price")
    private int sectionPrice;

    public static SectionPrice of(Stadium stadium, String seatSection, int sectionPrice){
        return SectionPrice.builder()
                .sectionStadium(stadium)
                .seatSection(seatSection)
                .sectionPrice(sectionPrice)
                .build();
    }
}
