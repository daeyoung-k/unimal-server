package com.unimal.map.service.geocoding

val geocodingTestJson = """
{
    "results": [
        {
            "place": "//places.googleapis.com/places/ChIJLyR7ySalfDURhxzLyJ8U0sE",
            "placeId": "ChIJLyR7ySalfDURhxzLyJ8U0sE",
            "location": {
                "latitude": 37.540973199999996,
                "longitude": 127.08679839999998
            },
            "granularity": "ROOFTOP",
            "viewport": {
                "low": {
                    "latitude": 37.539624219708493,
                    "longitude": 127.0854494197085
                },
                "high": {
                    "latitude": 37.542322180291492,
                    "longitude": 127.08814738029147
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구 아차산로55길 81",
            "postalAddress": {
                "regionCode": "KR",
                "languageCode": "en",
                "postalCode": "05040",
                "administrativeArea": "Seoul",
                "locality": "Gwangjin District",
                "addressLines": [
                    "81 Achasan-ro 55-gil"
                ]
            },
            "addressComponents": [
                {
                    "longText": "81",
                    "shortText": "81",
                    "types": [
                        "premise"
                    ]
                },
                {
                    "longText": "아차산로55길",
                    "shortText": "아차산로55길",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_4"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "05040",
                    "shortText": "05040",
                    "types": [
                        "postal_code"
                    ]
                }
            ],
            "types": [
                "street_address"
            ],
            "plusCode": {
                "globalCode": "8Q99G3RP+9P",
                "compoundCode": "G3RP+9P 대한민국 서울특별시"
            }
        },
        {
            "place": "//places.googleapis.com/places/ChIJ97gBzCalfDURSPw8lG0jJRk",
            "placeId": "ChIJ97gBzCalfDURSPw8lG0jJRk",
            "location": {
                "latitude": 37.5409612,
                "longitude": 127.0866332
            },
            "granularity": "GEOMETRIC_CENTER",
            "viewport": {
                "low": {
                    "latitude": 37.539612219708495,
                    "longitude": 127.08528421970851
                },
                "high": {
                    "latitude": 37.542310180291494,
                    "longitude": 127.08798218029149
                }
            },
            "formattedAddress": "대한민국 서울특별시 구의동 248-76번지 광진구 서울특별시 KR",
            "postalAddress": {
                "regionCode": "KR",
                "languageCode": "en",
                "postalCode": "05040",
                "addressLines": [
                    "구의동 248-76번지 광진구 서울특별시 KR"
                ]
            },
            "addressComponents": [
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "05040",
                    "shortText": "05040",
                    "types": [
                        "postal_code"
                    ]
                }
            ],
            "types": [
                "establishment",
                "point_of_interest",
                "school"
            ],
            "plusCode": {
                "globalCode": "8Q99G3RP+9M",
                "compoundCode": "G3RP+9M 대한민국 서울특별시"
            }
        },
        {
            "place": "//places.googleapis.com/places/ChIJQYruFSGlfDURs3HzfoGHAQE",
            "placeId": "ChIJQYruFSGlfDURs3HzfoGHAQE",
            "location": {
                "latitude": 37.5395697,
                "longitude": 127.0866995
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.5379224,
                    "longitude": 127.08540036970851
                },
                "high": {
                    "latitude": 37.5410182,
                    "longitude": 127.08809833029149
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.5379224,
                    "longitude": 127.0861621
                },
                "high": {
                    "latitude": 37.5410182,
                    "longitude": 127.0873366
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구 아차산로55길",
            "addressComponents": [
                {
                    "longText": "아차산로55길",
                    "shortText": "아차산로55길",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_4"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "political",
                "sublocality",
                "sublocality_level_4"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJRdu5LCGlfDUR-rOMlsjjULA",
            "placeId": "ChIJRdu5LCGlfDUR-rOMlsjjULA",
            "location": {
                "latitude": 37.5408044,
                "longitude": 127.0860342
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.539505169708505,
                    "longitude": 127.0846970697085
                },
                "high": {
                    "latitude": 37.5422031302915,
                    "longitude": 127.08739503029149
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.539695300000005,
                    "longitude": 127.0849688
                },
                "high": {
                    "latitude": 37.542013,
                    "longitude": 127.0871233
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구",
            "addressComponents": [
                {
                    "longText": "05040",
                    "shortText": "05040",
                    "types": [
                        "postal_code"
                    ]
                },
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "postalCodeLocalities": [
                {
                    "text": "Gwangjin District",
                    "languageCode": "en"
                }
            ],
            "types": [
                "postal_code"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJo5Wm1CalfDURLtcvxzc4ioQ",
            "placeId": "ChIJo5Wm1CalfDURLtcvxzc4ioQ",
            "location": {
                "latitude": 37.5414713,
                "longitude": 127.0857346
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.5367981,
                    "longitude": 127.0807244
                },
                "high": {
                    "latitude": 37.5451251,
                    "longitude": 127.09045839999997
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.5367981,
                    "longitude": 127.0807244
                },
                "high": {
                    "latitude": 37.5451251,
                    "longitude": 127.09045839999997
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구 구의제1동",
            "addressComponents": [
                {
                    "longText": "구의제1동",
                    "shortText": "구의제1동",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_2"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "political",
                "sublocality",
                "sublocality_level_2"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJTeMD9C-lfDUR_t7nvs2EaMY",
            "placeId": "ChIJTeMD9C-lfDUR_t7nvs2EaMY",
            "location": {
                "latitude": 37.5444007,
                "longitude": 127.09284400000001
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.5279496,
                    "longitude": 127.0794853
                },
                "high": {
                    "latitude": 37.5591856,
                    "longitude": 127.1049411
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.5279496,
                    "longitude": 127.0794853
                },
                "high": {
                    "latitude": 37.5591856,
                    "longitude": 127.1049411
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구 구의동",
            "addressComponents": [
                {
                    "longText": "구의동",
                    "shortText": "구의동",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_2"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "political",
                "sublocality",
                "sublocality_level_2"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJCRkc-CilfDURMf90Sfel8ag",
            "placeId": "ChIJCRkc-CilfDURMf90Sfel8ag",
            "location": {
                "latitude": 37.5451021,
                "longitude": 127.08542689999999
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.5225138,
                    "longitude": 127.05621470000001
                },
                "high": {
                    "latitude": 37.5737333,
                    "longitude": 127.11522579999999
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.5225138,
                    "longitude": 127.05621470000001
                },
                "high": {
                    "latitude": 37.5737333,
                    "longitude": 127.11522579999999
                }
            },
            "formattedAddress": "대한민국 서울특별시 광진구",
            "addressComponents": [
                {
                    "longText": "광진구",
                    "shortText": "광진구",
                    "types": [
                        "political",
                        "sublocality",
                        "sublocality_level_1"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "political",
                "sublocality",
                "sublocality_level_1"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJzWXFYYuifDUR64Pq5LTtioU",
            "placeId": "ChIJzWXFYYuifDUR64Pq5LTtioU",
            "location": {
                "latitude": 37.565212900000006,
                "longitude": 126.97735170000001
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.4282973,
                    "longitude": 126.7644837
                },
                "high": {
                    "latitude": 37.7014549,
                    "longitude": 127.1837949
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.4282973,
                    "longitude": 126.7644837
                },
                "high": {
                    "latitude": 37.7014549,
                    "longitude": 127.1837949
                }
            },
            "formattedAddress": "대한민국 서울특별시",
            "addressComponents": [
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "locality",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "locality",
                "political"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJzzlcLQGifDURm_JbQKHsEX4",
            "placeId": "ChIJzzlcLQGifDURm_JbQKHsEX4",
            "location": {
                "latitude": 37.550263,
                "longitude": 126.9970831
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 37.4282973,
                    "longitude": 126.7644837
                },
                "high": {
                    "latitude": 37.7014549,
                    "longitude": 127.1837949
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.4282973,
                    "longitude": 126.7644837
                },
                "high": {
                    "latitude": 37.7014549,
                    "longitude": 127.1837949
                }
            },
            "formattedAddress": "대한민국 서울특별시",
            "addressComponents": [
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "administrative_area_level_1",
                "political"
            ]
        },
        {
            "place": "//places.googleapis.com/places/ChIJm7oRy-tVZDURS9uIugCbJJE",
            "placeId": "ChIJm7oRy-tVZDURS9uIugCbJJE",
            "location": {
                "latitude": 35.907757,
                "longitude": 127.76692200000001
            },
            "granularity": "APPROXIMATE",
            "viewport": {
                "low": {
                    "latitude": 33.0041,
                    "longitude": 124.58629999999998
                },
                "high": {
                    "latitude": 38.634000000000007,
                    "longitude": 131.1603
                }
            },
            "bounds": {
                "low": {
                    "latitude": 33.0041,
                    "longitude": 124.58629999999998
                },
                "high": {
                    "latitude": 38.634000000000007,
                    "longitude": 131.1603
                }
            },
            "formattedAddress": "대한민국",
            "addressComponents": [
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "country",
                "political"
            ]
        },
        {
            "place": "//places.googleapis.com/places/GhIJngYMkj7FQkARt3u5T47FX0A",
            "placeId": "GhIJngYMkj7FQkARt3u5T47FX0A",
            "location": {
                "latitude": 37.540972,
                "longitude": 127.086811
            },
            "granularity": "GEOMETRIC_CENTER",
            "viewport": {
                "low": {
                    "latitude": 37.5395885197085,
                    "longitude": 127.0854635197085
                },
                "high": {
                    "latitude": 37.542286480291494,
                    "longitude": 127.08816148029148
                }
            },
            "bounds": {
                "low": {
                    "latitude": 37.540875,
                    "longitude": 127.08675000000001
                },
                "high": {
                    "latitude": 37.541,
                    "longitude": 127.08687499999999
                }
            },
            "formattedAddress": "대한민국 서울특별시 G3RP+9P",
            "addressComponents": [
                {
                    "longText": "G3RP+9P",
                    "shortText": "G3RP+9P",
                    "types": [
                        "plus_code"
                    ]
                },
                {
                    "longText": "서울특별시",
                    "shortText": "서울특별시",
                    "types": [
                        "administrative_area_level_1",
                        "political"
                    ],
                    "languageCode": "ko"
                },
                {
                    "longText": "대한민국",
                    "shortText": "KR",
                    "types": [
                        "country",
                        "political"
                    ],
                    "languageCode": "ko"
                }
            ],
            "types": [
                "plus_code"
            ],
            "plusCode": {
                "globalCode": "8Q99G3RP+9P",
                "compoundCode": "G3RP+9P 대한민국 서울특별시"
            }
        }
    ],
    "plusCode": {
        "globalCode": "8Q99G3RP+9PM",
        "compoundCode": "G3RP+9PM 대한민국 서울특별시"
    }
}
""".trimIndent()