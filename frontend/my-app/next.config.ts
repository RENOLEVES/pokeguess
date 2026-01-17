/** @type {import('next').Config} */
const nextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'raw.githubusercontent.com',
                pathname: '/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/**',
            },
        ],
    },
};

export default nextConfig;